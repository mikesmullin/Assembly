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

# disassembly example

    gdb hello
    info files
    disassemble 0x00000000004000b0,0x00000000004000cd
    x/s 0x6000d0
    break *0x00000000004000c4
    run
    continue
    quit
