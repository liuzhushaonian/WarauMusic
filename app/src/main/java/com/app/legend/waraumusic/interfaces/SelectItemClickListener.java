package com.app.legend.waraumusic.interfaces;

import com.app.legend.waraumusic.bean.PlayList;

public interface SelectItemClickListener {

    void click(int position, PlayList playList);

    void clickLast(String s);
}
