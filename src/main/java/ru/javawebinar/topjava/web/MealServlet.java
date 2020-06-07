package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;
import ru.javawebinar.topjava.storage.ListMealStorage;
import ru.javawebinar.topjava.storage.MapMealStorage;
import ru.javawebinar.topjava.storage.MealStorage;
import ru.javawebinar.topjava.util.MealsData;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {
    private static Logger log;
    private MealStorage mealStorage;

    @Override
    public void init() throws ServletException {
        log = getLogger(UserServlet.class);
        mealStorage = new MapMealStorage();
        super.init();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String uuid = request.getParameter("uuid");
        String date = request.getParameter("date");
        LocalDateTime ldt = LocalDateTime.parse(date);
        String calories = request.getParameter("calories");
        String description = request.getParameter("description");
        String action = request.getParameter("action");
        if (action.equals("update")) {
            log.debug("update post method. UUID: " + uuid);
            mealStorage.update(new Meal(uuid, ldt, description, Integer.parseInt(calories)));
        } else {
            log.debug("add post method. UUID: " + uuid);
            mealStorage.create(new Meal(uuid, ldt, description, Integer.parseInt(calories)));
        }
        response.sendRedirect("./meals");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        action = action == null ? "null" : action;
        switch (action) {
            case "delete":
                String uuid = request.getParameter("id");
                log.debug("Delete meal uuid: " + uuid + ". Redirect to ./meals");
                mealStorage.remove(uuid);
                response.sendRedirect("./meals");
                break;
            case "add":
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                LocalDateTime nowWithoutSeconds = LocalDateTime.parse(now, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Meal meal = new Meal(MealsUtil.generateUUID(), nowWithoutSeconds, "", 0);
                log.debug("Create meal uuid: " + meal.getId() + ". Forward to new.jsp");
                request.setAttribute("meal", meal);
                request.setAttribute("action", "add");
                request.getRequestDispatcher("jsp/new.jsp").forward(request, response);
                break;
            case "update":
                uuid = request.getParameter("id");
                log.debug("Update meal uuid: " + uuid + ". Forward to new.jsp");
                request.setAttribute("meal", mealStorage.get(uuid));
                request.setAttribute("action", "update");
                request.getRequestDispatcher("jsp/new.jsp").forward(request, response);
                break;
            case "fillPredefined":
                log.debug("fill Predefined from ./util/MealsData.java");
                mealStorage.addList(MealsData.getInstance().meals);
                response.sendRedirect("./meals");
                break;
            default:
                log.debug("forward to meals.jsp");
                String caloriesLimit = request.getParameter("limit");
                int caloriesLimitInt = caloriesLimit == null ? 2000 : Integer.parseInt(caloriesLimit);
                List<MealTo> filteredMeals = MealsUtil.filteredByStreams(mealStorage.getAll(), LocalTime.MIN, LocalTime.MAX, caloriesLimitInt);
                request.setAttribute("storage", MealsUtil.convertToMealTo(mealStorage.getAll(), filteredMeals));
                request.setAttribute("limit", caloriesLimitInt);
                request.getRequestDispatcher("jsp/meals.jsp").forward(request, response);
                break;
        }
    }
}
