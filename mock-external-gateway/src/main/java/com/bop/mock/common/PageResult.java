package com.bop.mock.common;

import java.util.List;

public record PageResult<T>(int pageNo, int pageSize, long total, List<T> records) {
}
