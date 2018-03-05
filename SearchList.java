/* 

interface SearchList: classes that use the file search feature shall implement this interface

used by Search.java 

18-11-2006 version 0.1.2: changed type of value to <int>

*/

package search;

public interface SearchList {  
	void addItem(String key, int value);
	void noItem();
};