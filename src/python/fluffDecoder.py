import json
from os import getcwd, listdir
from os.path import isfile, join
import base64
mypath = getcwd()

# grap only files from the current directory
onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
print(len(onlyfiles) - 1)
for fluff in onlyfiles:
    if fluff.endswith('.json'):
        data = json.load(open(fluff, 'r', encoding='utf-8'))
        fileName = data['name']
        # file in
        datafile = open(fileName + '.txt', 'w')
        datafile.write(data['data'])
        datafile.close()

        # file out
        decodedFile = open(fileName + 'decoded.txt', 'wb+')
        datafileB = open(fileName + '.txt', 'rb+')
        base64.decode(datafileB, decodedFile)
        datafileB.close()
        decodedFile.close()
