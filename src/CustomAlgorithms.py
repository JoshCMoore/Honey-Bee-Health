
import math
def toEnd(aList, pos, term,accessElement=lambda x:x):
    while pos in range(0,len(aList)) and accessElement(aList[pos]) == term:
        pos += 1
    return pos - 1


def BinarySearch(aList,term,accessElement=lambda x:x,toLastOccurence=True):
    start = 0
    stop = len(aList)-1
    while start != stop:
        middle = start + math.ceil((stop-start)/2.0)
        if accessElement(aList[middle]) == term:
            if toLastOccurence:
                return toEnd(aList,middle,term,accessElement=accessElement)
            else:
                return middle
        if accessElement(aList[middle]) < term:
            start = middle
            pass
        if accessElement(aList[middle]) > term:
            end = middle
            pass
    return -1
alist = [("a",0),("a",1),("a",1),("a",3),("a",4)]
access = lambda x: x[1]
#print access(alist[1])
print BinarySearch(alist,4,accessElement=access)