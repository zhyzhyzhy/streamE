# StreamE
Enhanced Java Stream

# 解决了什么问题
业务代码中，转换DO到DTO的过程中，需要聚合不同来源的数据。
比如，Article的DO中仅包含了作者id和CategoryId
```java
class Article {
    private Long id;
    private String name;
    private String content;
    private Long authorId;
    private Long categoryId;
}
```
而作者信息和Category信息在另外一张表中或者其他业务方提供
```java
class Author {
    private Long id;
    private String nickname;
    private String avatar;
}

class Category {
    private Long id;
    private String intro;
}
```

聚合的DTO需要包含作者和Category的详细信息：
```java
class ArticleModel {
    private Long id;
    private String name;
    private String content;
    private Author author;
    private Category category;
}
```

正常写法：
```java
    public List<ArticleModel> fetchByIds(List<Long> ids) {
        Map<Long, Article> articleMap = db.fetchArticles(ids);
        
        List<Long> authorIds = streamE(articleMap.values()).map(Article::getAuthorId).collect(Collectors.toList());
        Map<Long, Author> authorMap = db.fetchAuthors(authorIds);


        List<Long> categoryIds = streamE(articleMap.values()).map(Article::getCategoryId).collect(Collectors.toList());
        Map<Long, Category> categoryMap = db.fetchCategories(categoryIds);

        return streamE(articleMap.values()).map(article -> {
            ArticleModel articleModel = new ArticleModel();
            articleModel.setId(article.getId());
            //...
            articleModel.setAuthor(authorMap.get(article.getAuthorId()));
            articleModel.setCategory(categoryMap.get(article.getCategoryId()));
            return articleModel;
        }).collect(Collectors.toList());
    }
```

较为繁琐，本项目给stream增加了prepare方法，一定程度上解决了这个问题
```java
    public List<ArticleModel> fetchByIdsV2(List<Long> ids) {
        return streamE(db.fetchArticles(ids).values())
                .prepare("author", articles -> db.fetchAuthors(streamE(articles).map(Article::getAuthorId).collect(Collectors.toList())))
                .prepare("category", articles -> db.fetchCategories(streamE(articles).map(Article::getCategoryId).collect(Collectors.toList())))
                .map(article -> {
                    ArticleModel articleModel = new ArticleModel();
                    articleModel.setId(article.getId());
                    articleModel.setName(article.getName());
                    articleModel.setContent(article.getContent());
                    articleModel.setAuthor(StreamContext.of("author", article.getAuthorId()));
                    articleModel.setCategory(StreamContext.of("category", article.getCategoryId()));
                    return articleModel;
                })
                .collect(Collectors.toList());
    }
```

# TODO
1. 给prepare增加更多的配置，异步化等
2. 使使用更加便捷，现在使用不是很方便