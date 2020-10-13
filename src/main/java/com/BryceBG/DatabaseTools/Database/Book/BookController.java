package com.BryceBG.DatabaseTools.Database.Book;

import java.util.Map;

import com.BryceBG.DatabaseTools.utils.Path;
import com.BryceBG.DatabaseTools.utils.RequestUtil;
import com.BryceBG.DatabaseTools.utils.ViewUtil;

import io.javalin.http.Handler;

import static com.BryceBG.DatabaseTools.Database.InstantiatedDaos.*;//needed for instantiated userDao and bookDao


/**
 * This class acts as the web controller and deals with getting and handling the requests sent to the server for books.
 * @author Limited1
 *
 */
public class BookController {
	
	
	
    public static Handler fetchAllBooks = ctx -> {
        Map<String, Object> model = ViewUtil.baseModel(ctx);
        model.put("books", bookDao.getAllBooks());
        ctx.render(Path.Template.BOOKS_ALL, model);  
    };

    public static Handler fetchOneBook = ctx -> {
        Map<String, Object> model = ViewUtil.baseModel(ctx);
        String[] id = RequestUtil.getParamIdentifier(ctx);
        model.put("book", bookDao.getBookByIdentifier(id[0], id[1]));
        ctx.render(Path.Template.BOOKS_ONE, model);
    };
}
