import math
import random
from collections import namedtuple
import socket
import itertools
from difflib import SequenceMatcher
#Definitions
##
# MyAbstract is essentially a Struct representing a paper abstract.
# @param title String
# @param date datetime Publication date
# @param text String Body of the abstract
# @param keywords [String] List of key phrases
MyAbstract = namedtuple('MyAbstract',["title","date","text","keywords"])

MyCorpora = namedtuple('MyCorpora',["corpus","id2word"])
#End Declarations
####################################################################################################

#Public Functions
#Binary Search implemented routinely to avoid stack overflow
def binarySearch (arr,x,key=lambda e: e,toEnd=True):
    """
    Binary Search implemented routinely to avoid stack overflow.
    Args:
        arr:   [T], Array to perform the binary search over
        x:   T, Element searching for.
        key:   lambda k:->T, Lambda accessing the comparable component of each element. Default= lambda k: k
        toEnd:   Bool, Specifies whether routine should return the last occurence of x. 

    Returns:
        The index of x if it is in the array. Else -1

    Examples:
        >>> arr = [(1,0),(2,0),(3,0)]
        >>> binarySearch(arr,3,key=lambda k: k[0])
        2
    """
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
    """
    Flattens an array of arrays.
    Args:
        aList:   Array of arrays
    Returns:
        Flattened array.
    """
    return list(itertools.chain.from_iterable(aList))

class WordMapper:
    """
    Maps a word (String) to a an id (int) for the graph

    Gephi uses integer IDs for its nodes. Therefore we have to map
    each word to an id to use it in the graph.
    """
    def __init__(self):
        """
        Constructor sets self.tag = 0 and creates a blank map(dict)
        Args:
            self:   Object to be initialized
        """
        self.tag = 0
        self.wordMap = {}
    def mapWord(self, word):
        """
        Maps a word to an integer

        If the word has not been encountered before, it is equated with slf.tag
        and self.tag is incremented. Else the tag associated to this particular word is returned.
        Args:
            word: String to be mapped
        Returns:
            The integer associated with this word

        Examples:
            >>> mapper = CustomApi.WordMapper()
            >>> mapper.mapWord("stuff")
            0
        """
        if word in self.wordMap.keys():
            return self.wordMap[word]
        else:
            self.wordMap[word] = self.tag
            self.tag += 1
            return self.tag-1


class YearOccurences:
    """
    """
    def __init__(self,phrase):
        """
        """
        self.years = {}
        self.phrase = phrase
        self.cooccurringPhrases = {}
    def update(self,year):
        """
        """
        if year in self.years.keys():
            self.years[year] = self.years[year] + 1
        else:
            self.years[year] = 1
    def sum(self):
        self.s = 0
        for (key,val) in self.years.iteritems():
            self.s += val
        return self.s
    def getPhrase(self):
        return self.phrase
    def peakYear(self):
        max = None
        for (key,val) in self.years.iteritems():
            if max == None:
                max = (key,val)
            elif val >= mav[1]:
                max = (key,val)
        return max
    def registerPhrases(self,phrases):
        for ph in phrases:
            if ph != self.phrase:
                if ph not in self.cooccurringPhrases.keys():
                     self.cooccurringPhrases[ph] = 1
                else:
                     self.cooccurringPhrases[ph] += 1
    def firstOccurence(self):
        try:
            return sorted(self.years.keys())[0]
        except:
            pass
        return None

class KeyWordTracker:
    def __init__(self):
        # Dict [keyphrase:YearlyOccurences] where a year is a (year,occurences)
        self.words = {}
    def track(self,phrase,year):
        if phrase in self.words.keys():
            self.words[phrase].update((year))
        else:
            yo = YearOccurences(phrase)
            yo.update(year)
            self.words[phrase] = yo
    def topN(self,topn=150):
        arr = sorted(self.words.values(),key=lambda k:k.sum(),reverse=True)
        return arr[0:topn]
    def registerCoOccurrences(self,phrase,phrases):
        if phrase in self.words.keys():
            self.words[phrase].registerPhrases(phrases)
                