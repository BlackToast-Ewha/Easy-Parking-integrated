'''
Code source: https://github.com/matthewearl/deep-anpr
'''

__all__ = (
    'train',
)
i = 0

import functools
import glob
import itertools
import multiprocessing
import random
import sys
import time

import cv2
import numpy
import tensorflow as tf

import common
import generate_training_set
import model

def code_to_vec(p, code):
    def char_to_vec(c):
        y = numpy.zeros((len(common.CHARS),))
        y[common.CHARS.index(c)] = 1.0
        return y

    c = numpy.vstack([char_to_vec(c) for c in code])

    return numpy.concatenate([[1. if p else 0], c.flatten()])


def read_data(img_glob):
    f=open('E:/project data/korListText.txt','r')
    line = f.readline()
    
    for fname in sorted(glob.glob(img_glob)):
        global i
        im = cv2.imread(fname)[:, :, 0].astype(numpy.float32) / 255.
        code = fname.split("\\")[1][-11:-5] #split("/")[1][9:16]을 바꿈
        p = fname.split("\\")[1][-5] == '1' #마찬가지로 /를 \\로 바꿈
        ind = fname.split("\\")[1][-14:-11]
        realind = int(ind)
        code = code[0:2]+line[realind]+code[2:]
        i+=1
        yield im, code_to_vec(p, code)

def unzip(b):
    xs, ys = zip(*b)
    xs = numpy.array(xs)
    ys = numpy.array(ys)
    return xs, ys

def batch(it, batch_size):
    out = []
    for x in it:
        out.append(x)
        if len(out) == batch_size:
            yield out
            out = []
    if out:
        yield out
      
def read_batches(batch_size):
    g = generate_training_set.generate_ims()
    def gen_vecs():
        for im, c, p in itertools.islice(g, batch_size):
            yield im, code_to_vec(p, c)

    while True:
        yield unzip(gen_vecs())

def get_loss(y, y_):
    # Calculate the loss from digits being incorrect.  Don't count loss from
    # digits that are in non-present plates.
    digits_loss = tf.nn.softmax_cross_entropy_with_logits(logits=tf.reshape(y[:, 1:],[-1, len(common.CHARS)]),labels=tf.reshape(y_[:, 1:],[-1, len(common.CHARS)]))
    digits_loss = tf.reshape(digits_loss, [-1, 7])
    digits_loss = tf.reduce_sum(digits_loss)
    digits_loss *= (y_[:, 0] != 0)
    digits_loss = tf.reduce_sum(digits_loss)

    # Calculate the loss from presence indicator being wrong.
    presence_loss = tf.nn.sigmoid_cross_entropy_with_logits(logits=y[:, :1], labels=y_[:, :1])
    presence_loss = 7 * tf.reduce_sum(presence_loss)

    return digits_loss, presence_loss, digits_loss + presence_loss


def train(learn_rate, report_steps, batch_size, initial_weights=None):
    '''
    plt_epoch = []
    plt_loss = []
    '''
    """
    Train the network.
    The function operates interactively: Progress is reported on stdout, and
    training ceases upon `KeyboardInterrupt` at which point the learned weights
    are saved to `weights.npz`, and also returned.
    :param learn_rate:
        Learning rate to use.
    :param report_steps:
        Every `report_steps` batches a progress report is printed.
    :param batch_size:
        The size of the batches used for training.
    :param initial_weights:
        (Optional.) Weights to initialize the network with.
    :return:
        The learned network weights.
    """
    x, y, params = model.get_training_model()

    y_ = tf.placeholder(tf.float32, [None, 7 * len(common.CHARS) + 1])

    digits_loss, presence_loss, loss = get_loss(y, y_)
    train_step = tf.train.AdamOptimizer(learn_rate).minimize(loss)

    best = tf.argmax(tf.reshape(y[:, 1:], [-1, 7, len(common.CHARS)]), 2)
    correct = tf.argmax(tf.reshape(y_[:, 1:], [-1, 7, len(common.CHARS)]), 2)

    if initial_weights is not None:
        assert len(params) == len(initial_weights)
        assign_ops = [w.assign(v) for w, v in zip(params, initial_weights)]

    init = tf.global_variables_initializer()

    def vec_to_plate(v):
        return "".join(common.CHARS[i] for i in v)

    def do_report():
        r = sess.run([best,
                      correct,
                      tf.greater(y[:, 0], 0),
                      y_[:, 0],
                      digits_loss,
                      presence_loss,
                      loss],
                     feed_dict={x: test_xs, y_: test_ys})
        num_correct = numpy.sum(
                        numpy.logical_or(
                            numpy.all(r[0] == r[1], axis=1),
                            numpy.logical_and(r[2] < 0.5,
                                              r[3] < 0.5)))
        r_short = (r[0][:190], r[1][:190], r[2][:190], r[3][:190])
        num_p_correct = numpy.sum(r[2] == r[3])
        print ("Batch:", batch_idx, "Digit loss:", r[4],
                                    "Presence_loss", r[5],
                                    "Loss:", r[6])
        '''
        plt_epoch.append(batch_idx)
        plt_loss.append(r[6])
        '''

    def do_batch():
        sess.run(train_step,
                 feed_dict={x: batch_xs, y_: batch_ys})
        if batch_idx % report_steps == 0:
            do_report()

    gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=0.95)
    with tf.Session(config=tf.ConfigProto(gpu_options=gpu_options)) as sess:
        sess.run(init)
        if initial_weights is not None:
            sess.run(assign_ops)

        test_xs, test_ys = unzip(list(read_data("E:/project data/test/*.png"))[:50])

        try:
            last_batch_idx = 0
            last_batch_time = time.time()
            batch_iter = enumerate(read_batches(batch_size))
            for batch_idx, (batch_xs, batch_ys) in batch_iter:
                do_batch()
                if batch_idx % report_steps == 0:
                    batch_time = time.time()
                    if last_batch_idx != batch_idx:
                        last_batch_idx = batch_idx
                        last_batch_time = batch_time

        except KeyboardInterrupt:
            '''
            plt.plot(plt_epoch, plt_loss)
            plt.xlabel('epoch')
            plt.ylabel('loss')
            plt.title('epoch-loss graph')
            fig = plt.gcf()
            plt.show()
            fig.savefig('loss_graph.pdf')
            '''
            last_weights = [p.eval() for p in params]
            numpy.savez("weights0417ver_mini_train.npz", *last_weights)
            return last_weights


if __name__ == "__main__":
    # 이미 학습을 진행시켜 놓은 npz 파일 있는 경우 추가학습에 해당하는 코드
    f = numpy.load("G:/ewha_project/weights/weights0417_1.npz")
    initial_weights = [f[n] for n in sorted(f.files,
                                                key=lambda s: int(s[4:]))]
    
    train(learn_rate=0.001,
          report_steps=20,
          batch_size=50,
          initial_weights=initial_weights)
