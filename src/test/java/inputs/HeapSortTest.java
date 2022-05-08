package inputs;

import soot.jimple.SubExpr;

// https://github.com/farhankhwaja/HeapSort/blob/master/HeapSort.java
public class HeapSortTest {
    // Build-Heap Function
    public static int buildheap(int []a){
        int n;
        n= 5 - 1;
        for(int i=n/2;i>=0;i--){
            maxheap(a,i, n);
        }
        return 0;
    }

    // Max-Heap Function
    public static int maxheap(int[] a, int i, int n){
        int left, right;
        left=2*i;
        right=2*i+1;
        int largest;
        // instrumented for reportWarnings
        int[] ignore = new int[5];
        int x = ignore[left]; // OKAY
        x = ignore[i]; // OKAY
        x = ignore[right]; // OKAY

        if(left <= n && a[left] > a[i]){
            largest =left;
        }
        else{
            largest =i;
        }

        if(right <= n && a[right] > a[largest]){
            largest =right;
        }
        if(largest !=i){
            exchange(a, i, largest);
            maxheap(a, largest, n);
        }
        x = ignore[largest]; // OKAY
        return 0;
    }

    // Exchange Function
    public static int exchange(int a[], int i, int j){
        int []ignore = new int[5]; // instrumented for reportWarnings
        int t=a[i];
        a[i]=a[j];
        a[j]=t;
        // instrumented for reportWarnings
        int x = ignore[i]; // OKAY
        int y = ignore[j]; // OKAY
        return 0;
    }

    public static void main(String[] args) {
        int []a= new int[5];
        a[0] = 3;
        a[1] = 5;
        a[2] = 1;
        a[3] = 2;
        a[4] = 4;
        int ignore = buildheap(a);
        int n = 5 - 1;
        for(int i=n;i>0;i--){
            int j = 0;
            ignore = exchange(a, i, j);
            n=n-1;
            ignore = maxheap(a, j, n);
        }
    }
}