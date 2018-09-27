'''
Code source: https://github.com/matthewearl/deep-anpr
'''
# -*- coding: utf-8 -*-

# hyperthreading 작업 코드 추가

__all__ = (
    'detect',
    'post_process',
)

import time
import collections
import math
import cv2
import numpy
import tensorflow as tf
import common
import model

import sys #npz 파일 가져오기 위해서
from threading import Thread # Hyperthreading 작업 위해서

def make_scaled_ims(im, min_shape):
    ratio = 1. / 2 ** 0.5
    shape = (im.shape[0] / ratio, im.shape[1] / ratio)

    while True:
        shape = (int(shape[0] * ratio), int(shape[1] * ratio))
        if shape[0] < min_shape[0] or shape[1] < min_shape[1]:
            break
        yield cv2.resize(im, (shape[1], shape[0]))

def detect(im, param_vals):
    """
    Detect number plates in an image.
    :param im:
        Image to detect number plates in.
    :param param_vals:
        Model parameters to use. These are the parameters output by the `train`
        module.
    :returns:
        Iterable of `bbox_tl, bbox_br, letter_probs`, defining the bounding box
        top-left and bottom-right corners respectively, and a 7,36 matrix
        giving the probability distributions of each letter.
    """

    # Convert the image to various scales.
    scaled_ims = list(make_scaled_ims(im, model.WINDOW_SHAPE))

    # Load the model which detects number plates over a sliding window.
    x, y, params = model.get_detect_model()

    # Execute the model at each scale.
    with tf.Session(config=tf.ConfigProto()) as sess:
        y_vals = []
        for scaled_im in scaled_ims:
            feed_dict = {x: numpy.stack([scaled_im])}
            feed_dict.update(dict(zip(params, param_vals)))
            y_vals.append(sess.run(y, feed_dict=feed_dict))

    # Interpret the results in terms of bounding boxes in the input image.
    # Do this by identifying windows (at all scales) where the model predicts a
    # number plate has a greater than 50% probability of appearing.
    #
    # To obtain pixel coordinates, the window coordinates are scaled according
    # to the stride size, and pixel coordinates.
    for i, (scaled_im, y_val) in enumerate(zip(scaled_ims, y_vals)):
        for window_coords in numpy.argwhere(y_val[0, :, :, 0] >
                                                       -math.log(1./0.99 - 1)):
            letter_probs = (y_val[0,
                                  window_coords[0],
                                  window_coords[1], 1:].reshape(
                                    7, len(common.CHARS)))
            letter_probs = common.softmax(letter_probs)

            img_scale = float(im.shape[0]) / scaled_im.shape[0]

            bbox_tl = window_coords * (8, 4) * img_scale
            bbox_size = numpy.array(model.WINDOW_SHAPE) * img_scale

            present_prob = common.sigmoid(
                               y_val[0, window_coords[0], window_coords[1], 0])

            yield bbox_tl, bbox_tl + bbox_size, present_prob, letter_probs


def _overlaps(match1, match2):
    bbox_tl1, bbox_br1, _, _ = match1
    bbox_tl2, bbox_br2, _, _ = match2
    return (bbox_br1[0] > bbox_tl2[0] and
            bbox_br2[0] > bbox_tl1[0] and
            bbox_br1[1] > bbox_tl2[1] and
            bbox_br2[1] > bbox_tl1[1])


def _group_overlapping_rectangles(matches):
    matches = list(matches)
    num_groups = 0
    match_to_group = {}
    for idx1 in range(len(matches)):
        for idx2 in range(idx1):
            if _overlaps(matches[idx1], matches[idx2]):
                match_to_group[idx1] = match_to_group[idx2]
                break
        else:
            match_to_group[idx1] = num_groups 
            num_groups += 1

    groups = collections.defaultdict(list)
    for idx, group in match_to_group.items():
        groups[group].append(matches[idx])

    return groups


def post_process(matches):
    """
    Take an iterable of matches as returned by `detect` and merge duplicates.
    Merging consists of two steps:
      - Finding sets of overlapping rectangles.
      - Finding the intersection of those sets, along with the code
        corresponding with the rectangle with the highest presence parameter.
    """
    groups = _group_overlapping_rectangles(matches)

    for group_matches in groups.values():
        present_probs = numpy.array([m[2] for m in group_matches])
        letter_probs = numpy.stack(m[3] for m in group_matches)

        yield letter_probs[numpy.argmax(present_probs)]


def letter_probs_to_code(letter_probs):
    return "".join(common.CHARS[i] for i in numpy.argmax(letter_probs, axis=1))

def detect_main(weight_filename):
    print("데이터셋 validating 시작")
    weight_path = 'D:/ewha_project/weights/' + weight_filename
    f = numpy.load(weight_path)  # weight 파일 load
    param_vals = [f[n] for n in sorted(f.files, key=lambda s: int(s[4:]))]

    # 정답 번호판 문자열이 담긴 txt 파일 load => 인식률 계산 위해
    targets = open('D:/ewha_project/test_data/target/target.txt', 'r')

    completely_correct = 0  # 모든 문자열이 맞은 사진 갯수
    number_correct = 0  # 번호판 문자열의 숫자만 맞은 사진 갯수
    total = 0  # 총 사진 갯수
    total_time = 0.

    # 원래 range(1, 99):
    for idx in range(1, 99):
        img_path = "D:/ewha_project/test_data/in" + str(idx) + ".jpg"
        print("detect.py 실행중입니다. 대상 이미지:", img_path)

        # 이미지 읽어오기
        img = cv2.imread(img_path)

        # resizing - 비율 기준으로
        # 정면, 정면-위쪽에서 찍힌 경우 가로 480px 필요 - in30.jpg, in 31.jpg
        # 측면으로 약간(좌측) 치우친 경우 500px 필요 - in32.jpg
        # 측면으로 심하게(우측) 치우친 경우 600px 필요 - in33.jpg

        # 사진 사이즈 조정 작업 => 시간 단축 + 메모리 문제
        if (img.shape[1] > 600):
            r = 600. / img.shape[1]
            dsize = (600, int(img.shape[0] * r))
            img = cv2.resize(img, dsize)

        # 사진 흑백으로 전환, normalize
        im_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY) / 255.

        # 사진 임계작업 => 실행시켜본 결과 인식률에는 크게 차이를 주지 않음.
        # ret, im_gray = cv2.threshold(im_gray, 127, 255, cv2.THRESH_TRUNC)
        # ret, im_gray = cv2.threshold(im_gray, 127, 255, cv2.THRESH_TOZERO)

        start_time = time.time()  # detection 시작 시간 측정

        # detection 작업 시작
        for letter_probs in post_process(detect(im_gray, param_vals)):
            code = letter_probs_to_code(letter_probs)
            code = code.replace(" ", "")  # code 문자열 공백 제거

            if code is "":
                print("예측이 제대로 이루어지지 않음.")
            else:
                print(code)

        end_time = time.time()
        runtime = end_time - start_time
        total_time += runtime
        print("detect에 걸린 시간:", runtime)  # 총 소요시간 출력

        target = targets.readline()  # target.txt(정답 문자열 담긴 파일)에서 한줄 읽어오기
        target = target.replace("\n", "")  # target 문자열 줄바꿈, 공백 제거

        # 문자열의 숫자부분만 추출
        code_number = code[:1] + code[3:]
        target_number = target[:1] + target[3:]

        # 인식률 계산
        if code == target:  # 문자열이 완벽하게 일치 시
            completely_correct += 1
            number_correct += 1
            print("completely correct!")
        elif code_number == target_number:  # 문자열 중 숫자 부분만 일치 시
            number_correct += 1
            print("partially correct!")
        else:  # 일치하지 않을 시
            print("not correct!")

        total += 1
        print("==================================================")

    # 인식률 출력
    print("전체 인식률:", float(completely_correct) / total, " 숫자 인식률:", float(number_correct) / total)
    print("평균 실행 소요시간:", total_time / total)

def thread_main(weight_filename):
    threads = []
    max_th = 4
    for thnum in range(max_th):
        t = Thread(target=detect_main, args=(weight_filename,)) # 해당 함수를 target으로 thread 생성
        threads.append(t)
        print("thread 생성:", thnum) # ==> 생성까지는 문제 없이 진행 됨

    # 각각의 thread를 시작하고, join
    for th in threads:
        th.start()
        print("thread 시작!", th.getName())
    for th in threads:
        th.join()

if __name__ == "__main__":
    weight = str(sys.argv[1])
    thread_main(weight)