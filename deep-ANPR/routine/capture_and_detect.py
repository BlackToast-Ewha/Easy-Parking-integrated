# #### [1] 아두이노에서 거리센서 값 받아오기
# #### [2] 일정 거리 안으로 차량이 근접하면
# #### [3] 차량 전면부 사진 촬영
# #### [4] 그 이미지를 input으로 detect.py 실행
# #### [5] 해당 결과를 txt 파일에 저장

import cv2
import os, sys, time
import numpy as np
import serial

sys.path.insert(0, os.path.dirname(__file__))
from deep_modules.detect_single import detect, post_process, letter_probs_to_code
from config import get_config

# get configurations
config = get_config()
weight_path = config.weight_path
dist_port = config.port
dist_bRate = config.bRate
outf = config.outf
java_gate_path = config.java_gate_path
java_intsc_path = config.java_intsc_path

# setup parameters for pretrained ANPR model
f = np.load(weight_path)
param_vals = [f[n] for n in sorted(f.files, key=lambda s: int(s[4:]))]

# setup serial communicator for distance sensor
dist_sr = serial.Serial(dist_port, dist_bRate)

# directory path configuration
if not os.path.exists(outf):
    os.makedirs(outf)

cnt = 0

while True:
    sensor_string = dist_sr.readline()[:-2].decode()
    sensor_string = sensor_string.split(':')[1]
    sensor_string = sensor_string[1:-2]
    sensor_dist = float(sensor_string) # distance from HR-04 sensor

    print("distance to coming car:", sensor_dist)

    if sensor_dist != 0.0 and sensor_dist <= 15.0 :
        imgname = os.path.join(outf, "saved_%d.png" % cnt) # setting image file name

        cap = cv2.VideoCapture(1) # open video capture
        ret, frame = cap.read()

        if ret:
            print("Take picture complete!")
            cap.release()
            cv2.imwrite(imgname, frame) # save image file
            print("Save picture complete!")
            cnt += 1
        else:
            print("Capture fail!")

        print("Capture complete!!")

        # reshape captured image
        if frame.shape[2] > 600:
            r = 600. / frame.shape[1]
            dsize = (600, int(frame.shape[0] * r))
            frame = cv2.resize(frame, dsize)

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY) / 255. # RGB -> BG
        start_time = time.time() # set start time

        # do detecting
        for letter_probs in post_process(detect(gray, param_vals)):
            code = letter_probs_to_code(letter_probs)
            code = code.replace(" ", "")  # remove space string

            if code is None:
                pass
            else:
                print(code)

        end_time = time.time()
        print("Detect number plate complete!")
        print("Detect processing time: %.3f" % (end_time-start_time))

        # save number plate detection result => gate directory
        with open(java_gate_path, 'w', encoding='euc-kr') as f:
            f.write(code)
            print("Save result in gate complete!")
            f.close()

        # save number plate detection result => intersection directory
        with open(java_intsc_path, 'w', encoding='euc-kr') as f:
            f.write(code)
            print("Save result in intersection complete!")
            f.close()

        print("Save car entrance information complete!!!")