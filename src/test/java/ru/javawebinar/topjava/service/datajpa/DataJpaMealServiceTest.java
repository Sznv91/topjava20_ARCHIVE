package ru.javawebinar.topjava.service.datajpa;

import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.service.AbstractMealServiceTest;

@ActiveProfiles(value = {"datajpa", "postgres"})
public class DataJpaMealServiceTest extends AbstractMealServiceTest {

}
