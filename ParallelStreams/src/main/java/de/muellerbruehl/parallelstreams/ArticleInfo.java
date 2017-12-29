package de.muellerbruehl.parallelstreams;

public class ArticleInfo {
  private final int _articleId;
  private long _quantity;
  private Money _amount; 

  public ArticleInfo (int articleId){
    _articleId = articleId;
    _amount = new Money();
  }
  public int getArticleId() {
    return _articleId;
  }

  public long getQuantity() {
    return _quantity;
  }

  public void setQuantity(long quantity) {
    _quantity = quantity;
  }

  public Money getAmount() {
    return _amount;
  }

  public void setAmount(Money amount) {
    _amount = amount;
  }
  
  public void addQuantity (long quantity){
    _quantity += quantity;
  }
  public void addPrice(long cents){
    _amount.add(cents);
  }
}
