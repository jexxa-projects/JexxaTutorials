package io.jexxa.tutorials.bookstore.applicationservice;

import io.jexxa.common.facade.logger.SLF4jLogger;
import io.jexxa.jexxatest.JexxaTest;
import io.jexxa.tutorials.bookstore.BookStore;
import io.jexxa.tutorials.bookstore.domain.book.Book;
import io.jexxa.tutorials.bookstore.domain.book.BookRepository;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.jexxa.jexxatest.JexxaTest.getJexxaTest;
import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BookStoreServiceMockTest
{
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );


    @Test
    void testMockInstance()
    {
        // Arrange
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required. It expects the class name your application!
        JexxaTest jexxaTest = getJexxaTest(BookStore.class);

        // register our mock repository
        jexxaTest.registerStub(BookRepositoryMock.class);

        //The mock instance uses the following values
        int expectedAmountOfBooks = 1; //initialized by the mock
        int expectedAmountOfBooksInStock = 0;//initialized by the mock

        // Request the objects needed for our tests
        var objectUnderTest       = jexxaTest.getInstanceOfPort(BookStoreService.class);   // 1. We need the object we want to test

        //Assert
        assertEquals( expectedAmountOfBooks, objectUnderTest.getBooks().size() );      // Perform assertion against the object we test
        assertEquals( expectedAmountOfBooksInStock, objectUnderTest.amountInStock(ANY_BOOK) ); // Perform assertion against the repository
    }


    /* This class is a mock repository used in our test */
    public static class BookRepositoryMock implements BookRepository
    {
        private final List<Book> books = new ArrayList<>();

        public BookRepositoryMock()
        {
            SLF4jLogger.getLogger(BookRepositoryMock.class).info("Create Mock-Registry");
            books.add(Book.newBook(ANY_BOOK));
        }

        @Override
        public void add(Book book) {
            books.add(book);
        }

        @Override
        public Book get(ISBN13 isbn13) {
            return books.stream()
                    .filter( element -> isbn13.equals(element.getISBN13()))
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public boolean isRegistered(ISBN13 isbn13) {
            return search(isbn13).isPresent();
        }

        @Override
        public Optional<Book> search(ISBN13 isbn13) {
            return books.stream()
                    .filter( element -> isbn13.equals(element.getISBN13()))
                    .findFirst();
        }

        @Override
        public void update(Book book) {
            // no updated required because we work on aggregate directly
        }

        @Override
        public void remove(ISBN13 isbn13) {
            search(isbn13).ifPresent(books::remove);
        }

        @Override
        public List<Book> getAll() {
            return books;
        }
    }

}