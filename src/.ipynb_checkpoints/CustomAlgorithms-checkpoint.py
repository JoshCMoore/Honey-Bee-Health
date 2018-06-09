
def toEnd(aList, pos, term,accessElement=lambda x:x):
    while accessElement(aList[pos]) == term:
        pos += 1
    return pos - 1


def BinarySearch(aList,term,start=0,stop=0,accessElement=lambda x:x,toLastOccurence=True):
    while start != stop:
        middle = (stop-start)/2
        print middle
        if accessElement(aList[middle]) == term:
            if toLastOccurence:
                return toEnd(aList,middle,term,accessElement)
            else:
                return middle
        if accessElement(aList[middle]) < term:
            start = middle
            pass
        if accessElement(aList[middle]) > term:
            end = middle
            pass
    return -1
