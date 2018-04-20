#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sys
import getopt
import binascii


def read_file(path):
    with open(path, 'rb') as f:
        return bytearray(f.read())


def write_file(data, path):
    with open(path, 'wb') as f:
        f.write(data)


def get_payload_length(hex_dex_data):
    return hex_dex_data[-8:]


def get_payload_apk(dex_data, payload_length):
    # 从后面开始获取源apk数据,记得去掉最后存长度的那几位
    apk_start = int(payload_length, 16)*-1
    return dex_data[apk_start - 4:-4]


def get_payload(path):
    dex_data = read_file(path)
    hex_dex_data = binascii.b2a_hex(dex_data)
    payload_length = get_payload_length(hex_dex_data)
    print payload_length
    print int(payload_length, 16)
    hex_apk_data = get_payload_apk(dex_data, payload_length)
    print len(hex_apk_data)
    write_file(hex_apk_data, './payload.apk')


def main(argv):
    dex_path = ''
    try:
        opts, args = getopt.getopt(argv, "hp:", ["dex_path=", ])
    except getopt.GetoptError:
        print '-p <dex path>'
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print '-p <dex path>'
            sys.exit()
        elif opt in ("-p", "--dex_path"):
            dex_path = arg

    get_payload(dex_path)


if __name__ == "__main__":
    main(sys.argv[1:])
