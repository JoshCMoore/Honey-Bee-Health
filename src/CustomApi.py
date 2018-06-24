import math
import random
from collections import namedtuple
import socket
import itertools
#Definitions
MyAbstract = namedtuple('MyAbstract',["title","date","text","keywords"])

MyCorpora = namedtuple('MyCorpora',["corpus","id2word"])
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
def wVectorEqual(vec1,vec2):
    #This is a special case where the order of the vector is discarded, each vector represents an undirected edge on the graph
    return (vec1[0] == vec2[0] and vec1[1] == vec2[1]) or (vec1[0]==vec2[1] and vec1[1] == vec2[0])

class WordMapper:
    def __init__(self):
        self.tag = 0
        self.wordMap = {}
    def mapWord(self, word):
        if word in self.wordMap.keys():
            return self.wordMap[word]
        else:
            self.wordMap[word] = self.tag
            self.tag += 1
            return self.tag-1