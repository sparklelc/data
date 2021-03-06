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

def cal_difference(line):
    length = len(line)
    result = np.zeros(length-1, int).tolist()
    for i in range(0, length-1):
        result[i] = line[i+1] - line[i]
    # result is shorter than line
    return result

def write_file(file_name, N, M, n, m):
    output = open("myN_" + file_name, 'w')
    N_diff = cal_difference(N)
    for i in range(0, len(N)-1):
        output.write(str(int(N_diff[i])))
        output.write("\t")
        output.write(str(int(N[i])))
        output.write("\t")
        output.write(str(int(M[i])))
        output.write("\t")
        output.write(str(int(n[i])))
        output.write("\t")
        output.write(str(int(m[i])))
        output.write("\r\n")
    output.close();

    output = open("myM_" + file_name, 'w')
    M_diff = cal_difference(M)
    for i in range(0, len(M) - 1):
        output.write(str(int(M_diff[i])))
        output.write("\t")
        output.write(str(int(M[i])))
        output.write("\t")
        output.write(str(int(N[i])))
        output.write("\t")
        output.write(str(int(m[i])))
        output.write("\t")
        output.write(str(int(n[i])))
        output.write("\r\n")
    output.close();

all = load("One_1_86400_50.txt", (1,2,3,4)).transpose()
sub = all[:, 0:30]
sub.astype(int)

N = sub[0:1, :].tolist()[0]
M = sub[1:2, :].tolist()[0]
n = sub[2:3, :].tolist()[0]
m = sub[3:4, :].tolist()[0]
N = cal_sum(N)
M = cal_sum(M)

write_file("One_1_86400_50.txt", N, M, n, m)