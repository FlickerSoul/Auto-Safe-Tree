public class Test{
    static int cycleCount = 0;
    static String resultString;

    public static void main(String[] args){
//        System.out.println(count("banana"));
//        System.out.println(count("abracadabra"));
        System.out.println(count("asdfghjklqwertyuiopzxcvbnmqwtaiemdlspqhd"));
        System.out.println(cycleCount);
        System.out.println(resultString);
    }


    private static int count(String target){
        final char[] letterArray = target.toCharArray();

        final int arrayLength = letterArray.length;
        final int head = -1;
        int result = 1;

        if(arrayLength == 1){
            return result;
        }

        for(int centerPosition = 0; centerPosition < arrayLength; centerPosition++){
            int left = centerPosition - 1;
            int right = centerPosition + 1;

            while(left > head && right < arrayLength){
                if(right + 1 - left < result) {
                    left--;
                    right++;
                    continue;
                }

                StringBuilder stringBuilder = new StringBuilder(target.substring(left, right + 1));
                String original = stringBuilder.toString();
                String reverseResult = stringBuilder.reverse().toString();
                if(original.equals(reverseResult)){
                    int length = stringBuilder.length();
                    if(result < length) {
                        result = length;
                        resultString = original;
                    }
                }

                left--;
                right++;

                cycleCount++;
            }

            left = centerPosition - 2;
            right = centerPosition + 2;

            while(right <= arrayLength){
                if(right + 1 - centerPosition < result) {
                    right += 2;
                    continue;
                }
                StringBuilder stringBuilder = new StringBuilder(target.substring(centerPosition, right));
                String original = stringBuilder.toString();
                String reverseResult = stringBuilder.reverse().toString();
                if(original.equals(reverseResult)){
                    int length = stringBuilder.length();
                    if(result < length) {
                        result = length;
                        resultString = original;
                    }
                }

                right += 2;
                cycleCount++;

            }

            while (left >= head){
                if(left == head)
                    left = 0;
                if(centerPosition + 1 - left < result){
                    left -= 2;
                    continue;
                }

                StringBuilder stringBuilder = new StringBuilder(target.substring(left, centerPosition));
                String original = stringBuilder.toString();
                String reverseResult = stringBuilder.reverse().toString();
                if(original.equals(reverseResult)){
                    int length = stringBuilder.length();
                    if(result < length) {
                        result = length;
                        resultString = original;
                    }
                }

                left -= 2;
                cycleCount++;

            }
            cycleCount++;

        }

        return result;
    }
}

