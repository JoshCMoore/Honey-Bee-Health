import math
import random
from collections import namedtuple
import socket
#Definitions
MyAbstract = namedtuple('MyAbstract',["title","date","text","keyWords"])

#Def go to end of binary search position
def toEnd(aList, pos, term,accessElement=lambda x:x):
    while pos in range(0,len(aList)) and accessElement(aList[pos]) == term:
        pos += 1
    return pos - 1

#Binary Search implemented routinely to avoid stack overflow
def BinarySearch(aList,term,accessElement=lambda x:x,toLastOccurence=True):
    start = 0
    stop = len(aList)-1
    while start != stop:
        change = int((stop-start)/2)
        if change == 0:
            return -1
        middle = start + change
        if accessElement(aList[middle]) == term:
            if toLastOccurence:
                return toEnd(aList,middle,term,accessElement=accessElement)
            else:
                return middle
        if accessElement(aList[middle]) < term:
            start = middle
            continue
        if accessElement(aList[middle]) > term:
            stop = middle
            continue
    return -1


class ComunicationServer:
    def __init__():
        this.s = socket.socket()
        print "Socket successfully created"
        this.port = 83
        this.s.bind(('', this.port))        
        print "socket binded to %s" %(this.port)
        this.s.listen(5)     
        print "socket is listening"

    def acceptClients():
        while True:
            c, addr = s.accept()     
            print 'Got connection from', addr
            c.send('Thank you for connecting')