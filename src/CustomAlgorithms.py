import math
import random



# Generic Binary Search implementation
# @param: aList
def BinarySearch(aList,term,accessElement=lambda x:x,toLastOccurence=True):
    def toEnd(aList, pos, term,accessElement=lambda x:x):
        while pos in range(0,len(aList)) and accessElement(aList[pos]) == term:
            pos += 1
        return pos - 1
    start = 0
    stop = len(aList)-1
    while start != stop:
        change = int((stop-start)/2)
        middle = start + change
        if accessElement(aList[middle]) == term:
            if toLastOccurence:
                return toEnd(aList,middle,term,accessElement=accessElement)
            else:
                return middle
        if change == 0:
            if accessElement(aList[start]) == term:
                return toEnd(aList,start,term,accessElement=accessElement)
            if accessElement(aList[stop]) == term:
                return toEnd(aList,stop,term,accessElement=accessElement) 
            return -1
        if accessElement(aList[middle]) < term:
            start = middle
            continue
        if accessElement(aList[middle]) > term:
            stop = middle
            continue
    return -1
alist = [(1,1),(2,2),(3,3),(3,3),(3,4)]
print(BinarySearch(alist,4,accessElement=lambda x: x[1]))