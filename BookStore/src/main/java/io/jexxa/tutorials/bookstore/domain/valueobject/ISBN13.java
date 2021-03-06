package io.jexxa.tutorials.bookstore.domain.valueobject;

import io.jexxa.addend.applicationcore.ValueObject;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/**
 * IMPORTANT NOTE: This is a simplified ISBN13 number which only validates the checksum because this is sufficient for this tutorial
 */
@ValueObject
public record ISBN13(String value)
{
    public ISBN13
    {
        Objects.requireNonNull(value);
        validateChecksum(value);
    }

    private void validateChecksum(String isbn13)
    {
        var digits = isbn13
                .replace("-","")
                .toCharArray();

        int digitSum = 0;

        for (int i = 0; i < digits.length - 1 ; ++i) //Exclude checksum value (which is at position digits.length -1)
        {

            var digitAsInt = Integer.parseInt(String.valueOf(digits[i]));
            if ( i % 2 == 0)
            {
                digitSum += digitAsInt;
            }
            else
            {
                digitSum += digitAsInt * 3;
            }
        }

        var calculatedCheckDigit = (10 - ( digitSum % 10 )) % 10;

        var expectedDigit =  Integer.parseInt(String.valueOf(digits[digits.length -1]));

        Validate.isTrue( calculatedCheckDigit == expectedDigit,
                "Invalid ISBN number: Expected checksum value is "
                        + calculatedCheckDigit
                        + " Given value is "
                        +  expectedDigit
        );
    }

}
