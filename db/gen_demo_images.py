# -*- coding: utf-8 -*-
"""
XVI・搭 演示图片生成脚本
生成 12 张纯色 PNG 到 server/upload/demo/，与 db/data.sql 中的 image_url 对应。
用法：python3 db/gen_demo_images.py
"""
import os
import struct
import zlib

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                   '..', 'server', 'upload', 'demo')

COLORS = {
    'black': (38, 38, 42),
    'white': (243, 243, 240),
    'gray': (140, 142, 148),
    'beige': (222, 208, 184),
    'blue': (58, 92, 138),
    'brown': (110, 78, 55),
    'cream': (247, 240, 226),
    'khaki': (186, 168, 130),
    'navy': (35, 45, 70),
}


def png(path, w, h, rgb):
    raw = b''
    for _ in range(h):
        raw += b'\x00' + bytes(rgb) * w

    def chunk(tag, data):
        c = tag + data
        return struct.pack('>I', len(data)) + c + struct.pack(
            '>I', zlib.crc32(c) & 0xffffffff)

    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0)
    with open(path, 'wb') as f:
        f.write(sig + chunk(b'IHDR', ihdr)
                + chunk(b'IDAT', zlib.compress(raw, 6)) + chunk(b'IEND', b''))


if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    for name, rgb in COLORS.items():
        png(os.path.join(OUT, name + '.png'), 300, 300, rgb)
    print('OK ->', os.path.abspath(OUT))
