package io.jexxa.tutorials.bookstorecn.infrastructure.drivingadapter.eventstream;

import io.jexxa.addend.infrastructure.DrivingAdapter;
import io.jexxa.common.facade.logger.SLF4jLogger;
import io.jexxa.esp.drivingadapter.TypedEventListener;
import io.jexxa.tutorials.bookstorecn.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstorecn.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstorecn.domain.book.ISBN13;

@DrivingAdapter
public class BookSoldOutListener extends TypedEventListener<ISBN13, BookSoldOut> {
    private final BookStoreService bookStoreService;
    public BookSoldOutListener(BookStoreService bookStoreService)
    {
        super(ISBN13.class, BookSoldOut.class);
        this.bookStoreService = bookStoreService;
    }
    @Override
    protected void onEvent(BookSoldOut value) {
        SLF4jLogger.getLogger(BookSoldOutListener.class).warn("Book with ISBN {} is sold out", value.isbn13());
    }

    @Override
    public String topic() {
        return "BookStore";
    }

}
