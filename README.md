**JMD is a general purpose Java bytecode deobfuscation tool**

## Travis CI
[![Build Status](https://travis-ci.org/contra/JMD.svg?branch=master)](https://travis-ci.org/contra/JMD)

## Usage

        java -jar JMD.jar <jarfile location> <transformer name> <debug true/false> <transformer specific args>

JMDGUI.exe provides an optional frontend to the command line interface.

## Examples

    Remove ZKM obfuscation: java -jar JMD.jar "C:/Files/Magic.jar" zkm true
    Remove Allatori obfuscation: java -jar JMD.jar "C:/Files/Magic.jar" allatori true
    Remove Allatori-Strong obfuscation: java -jar JMD.jar "C:/Files/Magic.jar" allatori-strong true
    Remove JShrink obfuscation: java -jar JMD.jar "C:/Files/Magic.jar" jshrink true
    Remove DashO obfuscation: java -jar JMD.jar "C:/Files/Magic.jar" dasho true
    Remove SmokeScreen obfuscation: java -jar JMD.jar "C:/Files/Magic.jar" smokescreen true
    Remove Generic String obfuscation: java -jar JMD.jar "C:/Files/Magic.jar" genericstringdeobfuscator true
        
    Find all instances of example.com: java -jar JMD.jar "C:/Files/Magic.jar" stringscanner "example.com" true
    Replace all instances of example.com: java -jar JMD.jar "C:/Files/Magic.jar" stringreplacer "example.com" true "rscbunlocked.net"
    Renamer (currently breaks code): java -jar JMD.jar "C:/Files/Magic.jar" renamer true
    Correct stack issues: java -jar JMD.jar "C:/Files/Magic.jar" stackfixer true

## Download

Stable releases: https://github.com/Contra/JMD/downloads

Latest release (most likely stable): Get the files from the repo

## Requirements

JMD - Java 6 (hasn't been tested on anything else but should work)

JMDGUI - .NET 4 or higher
        
## License

Copyright (c) 2011 Contra <contra@australia.edu>

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

