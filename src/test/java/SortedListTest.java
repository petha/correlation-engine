import com.github.petha.correlationengine.model.SortedList;
import org.junit.Assert;
import org.junit.Test;

public class SortedListTest {
    @Test
    public void insertIntoEmptyList() {
        SortedList sortedList = new SortedList();
        int insert = sortedList.insert(1);
        Assert.assertEquals(0, insert);
    }

    @Test
    public void insertIntoListSmallFirst() {
        SortedList sortedList = new SortedList();
        int insert = sortedList.insert(1);
        Assert.assertEquals(0, insert);
        insert = sortedList.insert(2);
        Assert.assertEquals(1, insert);
    }

    @Test
    public void insertIntoListLargeFirst() {
        SortedList sortedList = new SortedList();
        int insert = sortedList.insert(2);
        Assert.assertEquals(0, insert);
        insert = sortedList.insert(1);
        Assert.assertEquals(0, insert);

        Assert.assertEquals(1, sortedList.get(0));
        Assert.assertEquals(2, sortedList.get(1));
    }

    @Test
    public void containsItemNotInList() {
        SortedList sortedList = new SortedList();
        int insert = sortedList.insert(2);
        Assert.assertEquals(0, insert);
        insert = sortedList.insert(1);

        Assert.assertEquals(-1, sortedList.contains(3));
    }
}
