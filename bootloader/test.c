#include <stdio.h>

int example(int a, int b) {
  return a + b + 3;
}

int main(void) {
  printf("%i\n", example(1, 2));
  return 0;
}
