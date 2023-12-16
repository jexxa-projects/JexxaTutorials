package io.jexxa.tutorials.bookstore.domain.book;

import io.jexxa.addend.applicationcore.BusinessException;

import java.io.Serial;

/**
 * Is thrown in case we try to sell a book currently not in stock
 */
@BusinessException
public class BookNotInStockException extends Exception
{
    @Serial
    private static final long serialVersionUID = 1L;
}
