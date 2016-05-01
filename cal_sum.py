# -*- coding: utf-8 -*-

import numpy as np

def load(file_name, columns, skip_row=0):
    return np.loadtxt(file_name, delimiter="\t", usecols=columns, skiprows=skip_row)

def cal_sum(line):
    length = len(line)
    sum = 0.0
    result = np.zeros(length, int).tolist()
    for i in range(0, length):
        sum = sum + line[i]
        result[i] = sum
    return result

def write_file(file_name, N, M):
    output = open("sum_" + file_name, 'w')
    for i in range(0, len(N)):
        output.write(str(int(N[i])))
        output.write("\t")
        output.write(str(int(M[i])))
        output.write("\r\n")
    output.close();



all = load("BiQgAqoOZ_Bj3dI7Lkz_%_86400_52.txt", (1,2)).transpose()
sub = all[:, 0:30]
sub.astype(int)

N = sub[0:1, :].tolist()[0]
M = sub[1:2, :].tolist()[0]
N = cal_sum(N)
M = cal_sum(M)

write_file("BiQgAqoOZ_Bj3dI7Lkz_%_86400_52.txt", N, M)