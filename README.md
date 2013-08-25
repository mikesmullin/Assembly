# Assembly

Follow along as we learn Assembly Language (ASM). Including x86, x86_64 architecture,
machine language, JVM bytecode, and fundamentals of hardware.


## Gather Tools (Ubuntu 12.04 64-bit)

```bash
# gcc and the linux source are all we need
sudo apt-get install linux-headers build-essential
# NOTICE: nasm, yasm, and third-party disassemblers are a waste of time

# ...but a good debugger like Evan's Debugger (EDB) is INVALUABLE! :)
sudo apt-get update && apt-get install libqt4-dev libboost1.48-all-dev subversion
svn checkout http://edb-debugger.googlecode.com/svn/trunk/ /tmp/edb-debugger
cd /tmp/edb-debugger
qmake
make
sudo make install
mkdir ~/.edb
sudo edb
# Once the program is launched, go Directories > Preferences, and set:
# Symbol Directory: ~/.edb
# Plugin Directory: /lib64/edb
# Session Directory: ~/.edb
# save and exit
```

## Assemble
Assuming you're following the example source in this repo.
```bash
# NOTICE: no git clone step; type it yourself--its fun!
gcc -c hello.s # i prefer the .asm extension but gcc requires this
```

## Link
```bash
ld -o hello hello.o
```

## Execute
```bash
./hello # should output "Hello, world!"
```

## Disassemble and Inspect
```bash
readelf -a hello
objdump -ds hello
nm hello
echo $(( 0xfffffffffffffffe )) # convert hex to 64-bit signed integer -2
```

## Debug Interactively
The easy way:
```bash
edb
# File > Open
# select your compiled binary
```

The hard way:
```bash
gdb hello
info files
disassemble 0x00000000004000b0,0x00000000004000cd
x/s 0x6000d0
break *0x00000000004000c4
run
continue
quit
```

## Reference
* https://gist.github.com/mikesmullin/6259449#comment-892678

