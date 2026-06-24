package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import java.util.List;

public interface SearchContentProvider {

    List<SearchSourceContent> listPublicContents();
}
