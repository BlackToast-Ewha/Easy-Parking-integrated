__all__ = (
    'DIGITS',
    'LETTERS',
    'CHARS',
    'sigmoid',
    'softmax',
)

import numpy


DIGITS = "0123456789"
LETTERS = "가나다라마거너더러머버서어저고노도로모보소오조구누두루무부수우주하호바사아자배"
CHARS = LETTERS + DIGITS

def softmax(a):
    exps = numpy.exp(a.astype(numpy.float64))
    return exps / numpy.sum(exps, axis=-1)[:, numpy.newaxis]

def sigmoid(a):
  return 1. / (1. + numpy.exp(-a))
