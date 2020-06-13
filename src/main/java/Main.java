import cc.lovezhy.streamE.Collectors;
import cc.lovezhy.streamE.StreamContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cc.lovezhy.streamE.EStreams.streamE;

public class Main {
    public static void main(String[] args) {
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(2L);
        ids.add(3L);
        List<ArticleModel> articleModels = new ArticleService().fetchByIdsV2(ids);
        System.out.println(articleModels);
    }
}


class ArticleService {
    private DB db = new DB();

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
}

class ArticleModel {
    private Long id;
    private String name;
    private String content;
    private Author author;
    private Category category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ArticleModel{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", author=").append(author);
        sb.append(", category=").append(category);
        sb.append('}');
        return sb.toString();
    }
}

class DB {

    private List<Author> authors;
    private List<Category> categories;
    private List<Article> articles;


    public DB() {
        authors = new ArrayList<>();
        authors.add(new Author(1L, "zhu", "a"));
        authors.add(new Author(2L, "mao", "b"));
        authors.add(new Author(3L, "zhuang", "c"));

        categories = new ArrayList<>();
        categories.add(new Category(1L, "c1"));
        categories.add(new Category(2L, "c2"));
        categories.add(new Category(3L, "c3"));

        articles = new ArrayList<>();
        articles.add(new Article(1L, "a1", "t1", 1L, 3L));
        articles.add(new Article(2L, "a1", "t1", 2L, 2L));
        articles.add(new Article(3L, "a1", "t1", 3L, 1L));
    }

    public Map<Long, Author> fetchAuthors(List<Long> ids) {
        return streamE(authors)
                .filter(author -> ids.contains(author.getId()))
                .collect(Collectors.toMap(Author::getId, author -> author));
    }

    public Map<Long, Category> fetchCategories(List<Long> ids) {
        return streamE(categories)
                .filter(category -> ids.contains(category.getId()))
                .collect(Collectors.toMap(Category::getId, category -> category));
    }

    public Map<Long, Article> fetchArticles(List<Long> ids) {
        return streamE(articles)
                .filter(article -> ids.contains(article.getId()))
                .collect(Collectors.toMap(Article::getId, article -> article));
    }
}

class Article {
    private Long id;
    private String name;
    private String content;
    private Long authorId;
    private Long categoryId;

    public Article(Long id, String name, String content, Long authorId, Long categoryId) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.authorId = authorId;
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Article{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", authorId=").append(authorId);
        sb.append(", categoryId=").append(categoryId);
        sb.append('}');
        return sb.toString();
    }
}

class Author {
    private Long id;
    private String nickname;
    private String avatar;

    public Author(Long id, String nickname, String avatar) {
        this.id = id;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Author{");
        sb.append("id=").append(id);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", avatar='").append(avatar).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

class Category {
    private Long id;
    private String intro;

    public Category(Long id, String intro) {
        this.id = id;
        this.intro = intro;
    }

    public Long getId() {
        return id;
    }

    public String getIntro() {
        return intro;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Category{");
        sb.append("id=").append(id);
        sb.append(", intro='").append(intro).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
