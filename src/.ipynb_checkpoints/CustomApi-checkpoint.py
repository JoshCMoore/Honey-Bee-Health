import math
import random
from collections import namedtuple
import socket
import itertools
#Definitions
MyAbstract = namedtuple('MyAbstract',["title","date","text"])
#End Declarations
####################################################################################################

#Def go to end of binary search position
def toLastOccurrence(aList, pos, term,key=lambda x:x):
    while pos in range(0,len(aList)) and key(aList[pos]) == term:
        pos += 1
    return pos - 1
#Public Functions
#Binary Search implemented routinely to avoid stack overflow
def binarySearch (arr,x,key=lambda e: e,toEnd=True):
    #Def go to end of binary search position
    def toLastOccurrence(aList, pos, term,key=lambda x:x):
        while pos in range(0,len(aList)) and key(aList[pos]) == term:
            pos += 1
        return pos - 1
    l = 0
    r = len(arr)-1
    # Check base case
    while r >= l:
        mid = l + (r - l)/2
        # If element is present at the middle itself
        if key(arr[mid]) == x:
            return mid if not toEnd else toLastOccurrence(arr,mid,x,key=key)
        # If element is smaller than mid, then it 
        # can only be present in left subarray
        elif key(arr[mid]) > x:
            r = mid+1
            continue
        # Else the element can only be present 
        # in right subarray
        else:
            l = mid+1
            continue
    # Element is not present in the array
    return -1

def flaten(aList):
    return list(itertools.chain.from_iterable(aList))


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