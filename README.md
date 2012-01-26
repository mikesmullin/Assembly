just teaching myself Assembly language

see also: http://asm.sourceforge.net/resources.html#tutorials

installing on Ubuntu 11.10 64-bit

# install linux kernel headers

    sudo apt-get install linux-headers

# install compiler

    sudo apt-get install nasm

# clone

    git clone git@github.com:mikesmullin/Assembly .

# compile and execute example

    make write.o && ./write
    make hello && ./hello
