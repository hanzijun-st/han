/**
 * 排序相关
 */
public class TestSort {
    public static void main(String[] args) {
        /*int[] nums ={1,3,5,2,6,8,0,4};
        int[] sort = sort(nums, 0, 7);

        for (int i : sort) {
            System.out.println(i);
        }*/
        merge2();
    }

    private static int[] sort(int[] nums, int low, int high) {
        int mid = (low + high) / 2;
        if (low < high) {
            // 左边
            sort(nums, low, mid);
            // 右边
            sort(nums, mid + 1, high);
            // 左右归并
            merge(nums, low, mid, high);
        }
        return nums;
    }

    static void merge(int[] nums, int low, int mid, int high){
        int[] copy = nums.clone();

        int k = low,i = low,j = mid + 1; //k表示从什么位置开始修改 i表示左半边起始位置,j表示右半边的起始位置

        while(k <= high){
            //1.左半边都处理完毕，只剩右边的数
            if(i > mid){
                nums[k++] = copy[j++];
            }
            //2.右半边的数都处理完毕了，只剩左边的数
            else if(j > high){
                nums[k++] = copy[i++];
            }
            //3.右边的数小于左边的数，将右边的数移到左边合适的位置
            else if(copy[j] < copy[i]){
                nums[k++] = copy[j++];
            }
            //4.左边的数小于右边的数，将左边的数移动到合适的位置
            else {
                nums[k++] = copy[i++];
            }
        }
    }

    static void merge2(){
        //int[] a ={2,3,4};
        //int[] numList = new int[nums.length+a.length];

        int a[] = {2,1,5,4};
        int b[] = {6,3,7,8};
        int c[]=new int[a.length+b.length];

        for(int i=0;i<a.length;i++){
            c[i]=a[i];
        }
        for(int j=0;j<b.length;j++){
            c[a.length+j]=b[j];
        }
        for(int k=0;k<c.length;k++){
            System.out.println(c[k]);
        }

    }
}