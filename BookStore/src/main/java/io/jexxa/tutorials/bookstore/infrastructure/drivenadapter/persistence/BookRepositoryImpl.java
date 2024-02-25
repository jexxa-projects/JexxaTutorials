package io.jexxa.tutorials.bookstore.infrastructure.drivenadapter.persistence;

import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.common.drivenadapter.persistence.repository.IRepository;
import io.jexxa.tutorials.bookstore.domain.book.Book;
import io.jexxa.tutorials.bookstore.domain.book.BookRepository;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static io.jexxa.common.drivenadapter.persistence.RepositoryFactory.createRepository;


@SuppressWarnings("unused")
@DrivenAdapter
public class BookRepositoryImpl implements BookRepository
{
    private final IRepository<Book, ISBN13> repository;

    public BookRepositoryImpl(Properties properties)
    {
        this.repository = createRepository(Book.class, Book::getISBN13, properties);
    }

    @Override
    public void add(Book book)
    {
        repository.add(book);
    }

    @Override
    public void update(Book book)
    {
        repository.update(book);
    }

    @Override
    public void remove(ISBN13 isbn13) {
        repository.remove(isbn13);
    }

    @Override
    public Book get(ISBN13 isbn13)
    {
        return repository.get(isbn13).orElseThrow();
    }

    @Override
    public boolean isRegistered(ISBN13 isbn13)
    {
        return search(isbn13).isPresent();
    }

    @Override
    public Optional<Book> search(ISBN13 isbn13)
    {
        return repository.get(isbn13);
    }

    @Override
    public List<Book> getAll()
    {
        return repository.get();
    }
}
