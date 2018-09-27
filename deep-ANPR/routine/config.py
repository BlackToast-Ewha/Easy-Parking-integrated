import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--weight_path', type=str, required=True, default='finalweight.npz', help='path to trained deep-model weight file, default=finalweight.npz')
parser.add_argument('--port', type=str, required=True, default='COM4', help='port number connected with Arduino board, default=COM4')
parser.add_argument('--bRate', type=int, required=True, default=115200, help='board rate for Arduino serial communication, default=115200')
parser.add_argument('--outf', type=str, required=True, default='InputImages', help='output folder to save captured images, default=InputImages')
parser.add_argument('--java_gate_path', type=str, required=True,
                    default='../../parking-guidance',
                    help='output folder to save car information which passed gate, default=InputImages')
parser.add_argument('--java_intsc_path', type=str, required=True,
                    default='../../parking-guidance',
                    help='output folder to save car information which passed intersection, default=InputImages')


def get_config():
    return parser.parse_args()

