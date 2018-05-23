#!/usr/bin/python
# -*- coding: UTF-8 -*-

import sys
import getopt
import binascii
import os
from zlib import adler32
from hashlib import sha1

from shield_manifest import shield_manifest


def change_str(start, end, src_str, add_str):
    return src_str[0:start] + add_str + src_str[end:]


def fix_dex_header(hex_data):
    bin_data = binascii.a2b_hex(hex_data)
    # 修复头中的checksum signature file_size信息
    # 按照顺序,修复file_size,signature,checksum,因为后面两个需要基于前面的数据来计算
    # read magic
    magic_set = 0
    magic_offset = 8 * 2
    magic = hex_data[magic_set:magic_offset]
    if magic != '6465780a30333500':
        print 'magic error'
        sys.exit()
    m = magic.decode('hex').split('\n')
    #  print 'magic :', m[0]
    #  print 'version :', m[1]

    # read checksum
    checksum_set = magic_offset
    checksum_offset = checksum_set + 4 * 2
    checksum = endan_little(hex_data[checksum_set:checksum_offset])
    # print 'checksum :', checksum

    # read signature
    signature_set = checksum_offset
    signature_offset = signature_set + 20 * 2
    signature = hex_data[signature_set:signature_offset]
    # print 'signature :', signature

    # read file_size 这里的filesize是从头读出来的,我们需要自己算,然后再写回去,也要注意大小端
    file_size_set = signature_offset
    file_size_offset = file_size_set + 4 * 2
    file_size = int(endan_little(hex_data[file_size_set:file_size_offset]), 16)
    # print 'file_size :', file_size

    # 计算新的文件大小
    new_file_size = len(bin_data)
    # 计算新的文件大小并处理大小端问题,准备填回去
    new_file_size_hex = endan_little('00' + hex(new_file_size)[2:])
    # hex_data[file_size_set:file_size_offset] = new_file_size_hex
    hex_data = change_str(file_size_set, file_size_offset, hex_data, new_file_size_hex)

    # 计算新的signature值
    signature_data = binascii.a2b_hex(hex_data[signature_offset:])
    new_signature = sha1(signature_data).hexdigest()
    hex_data = change_str(signature_set, signature_offset, hex_data, new_signature)

    # 计算新的checksum值
    data_for_checksum = hex(adler32(binascii.a2b_hex(hex_data[checksum_offset:])))
    data_for_checksum = data_for_checksum[data_for_checksum.find('x') + 1:]
    new_checksum = endan_little(data_for_checksum)
    # print 'new checksum :',new_checksum
    hex_data = change_str(checksum_set, checksum_offset, hex_data, new_checksum)

    # 至此,dex头部修复完成
    return hex_data


def endan_little(data):
    list = []
    for i in range(0, len(data), 2):
        list.append(data[i] + data[i + 1])
    list.reverse()
    return ''.join(list)


#
# def unzip_apk(path):
#     # 重命名apk为zip
#     if path[-4:len(path)] != '.apk':
#         print 'shield file error ,is not a apk file.'
#
#     zip_name = path[:-4] + '.zip'
#     # os.rename(path, zip_name)
#     command = 'cp ' + path + ' ' + zip_name
#     os.system(command)
#     # 解压
#     zip_file = zipfile.ZipFile(zip_name)
#     shield_tmp_path = 'shield_tmp'
#     if os.path.exists(shield_tmp_path) is False:
#         os.mkdir(shield_tmp_path)
#
#     for name in zip_file.namelist():
#         zip_file.extract(name, 'shield_tmp/')
#
#     dex_file_path = './' + shield_tmp_path + '/classes.dex'
#     # 返回dex数据路径
#     zip_file.close()
#     return dex_file_path


def add_new_dex_to_payload(org_payload_apk_path):
    # 用新的dex数据 替换dex中的数据 新的dex必须在当前目录下面,所以这里就直接写死了
    command = 'mv shield_dex.dex  classes.dex;zip -m ' + org_payload_apk_path + ' classes.dex'
    print command
    os.system(command)


def read_file(path):
    with open(path, 'rb') as f:
        return bytearray(f.read())


def write_file(data, path):
    with open(path, 'wb') as f:
        f.write(data)
    pass


# 这里可以对源文件进行加密,这只是一个demo,所以这里就不做什么操作了
def encrypt_payload(payload_data):
    return payload_data


def get_dex_from_payload_apk(payload_path):
    command = 'unzip ' + payload_path + ' classes.dex ; mv classes.dex payload_dex.dex'
    print command
    os.system(command)
    return 'payload_dex.dex'


def get_dex_from_shield_apk(shield_path):
    command = 'unzip ' + shield_path + ' classes.dex ; mv classes.dex shield_dex.dex'
    print command
    os.system(command)
    return 'shield_dex.dex'


def add_payload_to_shield(shield_path, payload_path):
    # 解压壳apk
    # print 'unzip shield apk'
    # dex_path = unzip_apk(shield_path)
    dex_path = get_dex_from_shield_apk(shield_path)
    print 'add source apk data'
    # 将源apk拼接到壳apk后面
    payload_file = read_file(get_dex_from_payload_apk(payload_path))
    # payload_file = read_file(payload_path)
    payload_data = binascii.b2a_hex(payload_file)
    dex_data = binascii.b2a_hex(read_file(dex_path))

    # 这里按照大端把长度数据给填进去了,没有做小端处理,填进去的payload数据也是一样,回头解析出来的时候也得用大端
    # 大小只有6字节,这里加个00当作对齐,默认大小是4字节
    hex_len_payload = '00' + hex(len(payload_file))[2:]
    print 'payload length :', hex_len_payload

    dex_data = dex_data + encrypt_payload(payload_data) + hex_len_payload

    print 'all data length ', len(dex_data)

    # 修复dex
    print 'fix header'
    fix_dex_header(dex_data)

    print 'write data to shell dex '
    # 将数据写回壳dex

    classes_dex_data = binascii.a2b_hex(dex_data)
    # print classes_dex_data
    write_file(classes_dex_data, "./shield_dex.dex")

    add_new_dex_to_payload(payload_path)
    write_file(classes_dex_data, "./shield_dex.dex")
    print 'success'


def shield(shield_path, payload_path):
    add_payload_to_shield(shield_path, payload_path)
    shield_manifest(payload_path)


def main(argv):
    shield_path = ''
    payload_path = ''
    try:
        opts, args = getopt.getopt(argv, "hs:p:", ["shield_path=", "payload_path="])
    except getopt.GetoptError:
        print '-s <shield apk path>', '-p <payload apk path>'
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print '-s <shield apk path>', '-p <payload apk path>'
            sys.exit()
        elif opt in ("-s", "--shield_path"):
            shield_path = arg
        elif opt in ("-p", "--payload_path"):
            payload_path = arg

    shield(shield_path, payload_path)


if __name__ == "__main__":
    main(sys.argv[1:])
