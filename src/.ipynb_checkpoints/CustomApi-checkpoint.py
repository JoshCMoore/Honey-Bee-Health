import math
import random
def toEnd(aList, pos, term,accessElement=lambda x:x):
    while pos in range(0,len(aList)) and accessElement(aList[pos]) == term:
        pos += 1
    return pos - 1


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