# -*- coding: utf-8 -*-

import math
import random
import sys
import numpy as np
from scipy.optimize import leastsq
import pylab as pl


def load(file_name, columns, skip_row=0):
    return np.loadtxt(file_name, delimiter="\t", usecols=columns, skiprows=skip_row)

""" logistic
"""
def logistic(t, p):
    r, K = p
    C1=0
    length = len(t)
    result = np.zeros(length, float)
    for i in range(0, length):
        try:
            tem = math.exp(C1-r*t[i]) + 1
            if tem == 0:
                tem = 1000000000.1
            else:
                tem = -K/tem
            result[i] = tem
        except Exception as e:
            result[i] = 0.00001
            print e
    return result
    
def relogistic(p, y, t):
    r, K = p
    length = len(t)
    result = np.zeros(length, float)
    log_result = logistic(t, p)
    for i in range(0, length):
        if abs(r) > 0.5:
            result[i] = sys.maxint
        else:
            result[i] = (y[i] - log_result[i])**2
    return result


""" lotka
"""
def relotka(p, N, M):
    """
    N M is the true number.
    """
    a, b, c, d, e, f = p
    length = len(N)
    result = np.zeros(length, float)
    # result[0] could not calculate.
    # result[0] = random.randint(2,10)/10000.0
    for i in range(1, length):
        errorN = abs(N[i] - N[i-1] - (a*N[i-1]+b*N[i-1]*N[i-1]+c*M[i-1]*N[i-1]))
        errorM = abs(M[i] - M[i-1] - (d*M[i-1]+e*M[i-1]*M[i-1]+f*M[i-1]*N[i-1]))
        result[i] = errorN + errorM
    #print "result:", result
    return result

def rmse_lotka(p, N, M):
    a, b, c, d, e, f = p
    length = len(N)
    total = 0.0
    diff_N = np.zeros(length, float)
    diff_M = np.zeros(length, float)
    for i in range(1, length):
        # todo N_sim[i] could be calculate by N[i-1] or N_sim[i-1]
        N_cal = N[i-1] + (a*N[i-1]+b*N[i-1]*N[i-1]+c*M[i-1]*N[i-1])
        M_cal = M[i-1] + (d*M[i-1]+e*M[i-1]*M[i-1]+f*M[i-1]*N[i-1])
        
        if not N[i] == 0:
            errorN = (abs(N[i] - N_cal)/N[i])**2
        else:
            errorN = (abs(N[i] - N_cal)/(N[i]+1))**2
            
        if not M[i] == 0:    
            errorM = (abs(M[i] - M_cal)/M[i])**2
        else:
            errorM = (abs(M[i] - M_cal)/(M[i]+1))**2
            
        diff_N[i] = errorN
        diff_M[i] = errorM
        total = total + errorN + errorM
        #print "total:", total, errorN, errorM
    return math.sqrt(total)/(len(N)+len(M))

def get_lotka(p, N, M):
    a, b, c, d, e, f = p
    length = len(N)
    N_sim = np.zeros(length, float)
    M_sim = np.zeros(length, float)
    N_sim[0] = N[0]
    M_sim[0] = M[0]
    for i in range(1, length):
        # todo N_sim[i] could be calculate by N[i-1] or N_sim[i-1]
        N_sim[i] = N_sim[i-1] + a*N_sim[i-1] + b*N_sim[i-1]*N_sim[i-1] + c*N_sim[i-1]*M_sim[i-1]
        M_sim[i] = M_sim[i-1] + d*M_sim[i-1] + e*M_sim[i-1]*M_sim[i-1] + f*N_sim[i-1]*M_sim[i-1]
    return {"N": N_sim, "M": M_sim}

""" other
"""
def rmse(line_true, line2):
    line3 = line_true - line2
    sum = 0.0
    for i in range(0, len(line2)):
        if not line_true[i] == 0: 
            #sum = sum + (float(line3[i])/line_true[i])**2
            sum = sum + (float(line3[i])) ** 2
        else:
            sum = sum + (float(line3[i])/1)**2
    sum = math.sqrt(sum)/len(line2)
    return sum

# cal sum
# input  1,2,3
# output 1,3,6
def cal_sum(line):
    length = len(line)
    sum = 0.0
    result = np.zeros(length, float).tolist()
    for i in range(0, length):
        sum = sum + line[i]
        result[i] = sum
    return result

###=============================================== 
###=============================================== 
###=============================================== 
"""
def func(x, p):
    #数据拟合所用的函数: A*sin(2*pi*k*x + theta)
    A, k, theta = p
    #print "p_1:", p
    return A*np.sin(2*np.pi*k*x+theta) 

def residuals(p, y, x):
    #实验数据x, y和拟合函数之间的差，p为拟合需要找到的系数
    return y - func(x, p)
 
A, k, theta = 10, 0.34, np.pi/6 # 真实数据的函数参数
x = np.linspace(0, -2*np.pi, 100)
y0 = func(x, [A, k, theta]) # 真实数据
y1 = y0 + 2 * np.random.randn(len(x)) # 加入噪声之后的实验数据 
p0 = [7, 0.2, 0] # 第一次猜测的函数拟合参数

# 调用leastsq进行数据拟合
# residuals为计算误差的函数
# p0为拟合参数的初始值
# args为需要拟合的实验数据

plsq = leastsq(residuals, p0, args=(y1, x))

print u"真实参数:", [A, k, theta] 
print u"拟合参数", plsq[0] # 实验数据拟合后的参数

pl.plot(x, y0, label=u"真实数据")
pl.plot(x, y1, label=u"带噪声的实验数据")
pl.plot(x, func(x, plsq[0]), label=u"拟合数据")
pl.legend()
pl.show()
"""
###=============================================== 
###=============================================== 
###=============================================== 


all = load("BiQgAqoOZ_Bj3dI7Lkz_%_86400_52.txt", (1,2)).transpose()
sub = all[:, 0:30]
sub.astype(int)
N = sub[0:1, :].tolist()[0]
M = sub[1:2, :].tolist()[0]
N = cal_sum(N)
M = cal_sum(M)
x_l = range(1, len(N) + 1)




print sub
print N
print M
p_lot = [2, 1, 1, 2, 1, 1]
plsq_lot = leastsq(relotka, p_lot, args=(N, M))
p0_lot = plsq_lot[0]
print "******************************"
print p0_lot
print "final diff :", rmse_lotka(p0_lot, N, M)

"""
x = np.linspace(0, len(N), len(N))
pl.plot(x, N, label="real")
pl.plot(x, get_lotka(p0_lot, N, M)["N"], label="sim")
pl.legend()
pl.show()
"""
#print rmse(np.array([1,2,3]), np.array([1,2,2]))






p_log = [1, -4000]

plsq_log_n = leastsq(relogistic, p_log, args=(N, x_l))
p0_log_n = plsq_log_n[0]
print p0_log_n
print "final diff N:", rmse(N, logistic(x_l, p0_log_n))

x = np.linspace(0, len(N), len(N))
pl.plot(x, N, label="real")
pl.plot(x, logistic(x, p0_log_n), label="sim")
print logistic(x, p0_log_n)
pl.legend()
pl.show()





plsq_log_m = leastsq(relogistic, p_log, args=(M, x_l))
p0_log_m = plsq_log_m[0]
print p0_log_m
print "final diff M:", rmse(M, logistic(x_l, p0_log_m))

x = np.linspace(0, len(M), len(M))
pl.plot(x, M, label="real")
pl.plot(x, logistic(x, p0_log_m), label="sim")
print logistic(x, p0_log_m)
pl.legend()
pl.show()