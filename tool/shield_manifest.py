#!/usr/bin/python
# -*- coding: UTF-8 -*-

import sys
import getopt
import os

try:
    import xml.etree.cElementTree as ET
except ImportError:
    import xml.etree.ElementTree as ET


def repackage_payload_apk():
    command = 'apktool b apkshield_tmp -o tmp.apk'
    os.system(command)


def get_manifest_from_repkg_apk():
    command = 'unzip tmp.apk AndroidManifest.xml'
    os.system(command)


def get_manifest(apk_path):
    command = 'apktool d ' + apk_path + ' -o apkshield_tmp'
    os.system(command)
    return './apkshield_tmp/AndroidManifest.xml'


def add_new_manifest_to_apk(path):
    command = 'zip -m ' + path + ' AndroidManifest.xml'
    os.system(command)


def read_file(path):
    with open(path, 'r') as f:
        return f.read()


def modify_manifest(path):
    ET.register_namespace('android', "http://schemas.android.com/apk/res/android")
    shield_application_name = 'com.egguncle.shield.ShieldApplication'
    tree = ET.parse(path)
    root = tree.getroot()

    #package_name = root.get('package')
    for child in root:
        if child.tag == 'application':
            application_tag = child
            application_tag.set('name', shield_application_name)
            element = ET.Element('meta-data')
            element.set('android:name','APPLICATION_CLASS_NAME')
            element.set('android:value','com.egguncle.apkshield.MyApplication')
            child.append(element)
            print '---'

    tree.write(path)
    return path


def shield_manifest(apk_path):
    print 'get manifest'
    manifest_path = get_manifest(apk_path)
    print 'start to modify AndroidManifest.xml'
    modify_manifest(manifest_path)
    print 'repackage apk'
    repackage_payload_apk()
    print 'get manifest from repkg apk'
    get_manifest_from_repkg_apk()
    print 'add new manifest to apk'
    add_new_manifest_to_apk(apk_path)


def main(argv):
    try:
        opts, args = getopt.getopt(argv, "hp:", ["payload_path="])
    except getopt.GetoptError:
        print '-p <payload apk path> '
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print '-p <payload apk path> '
            sys.exit()
        elif opt in ("-p", "--payload_path"):
            payload_path = arg
            shield_manifest(payload_path)


if __name__ == "__main__":
    main(sys.argv[1:])
