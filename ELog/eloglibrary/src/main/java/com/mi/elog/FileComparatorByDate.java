package com.mi.elog;

import java.io.File;
import java.util.Comparator;

public class FileComparatorByDate implements Comparator<File> {

	@Override
	public int compare(File arg0, File arg1) {
		long diff = arg0.lastModified() - arg1.lastModified();
		if (diff > 0)
			return 1;
		else if (diff == 0)
			return 0;
		else
			return -1;
	}

}
