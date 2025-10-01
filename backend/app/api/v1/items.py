from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.schemas.item import ItemVoteRequest, ItemVoteResponse
from app.models.item import Item

router = APIRouter()


@router.post("/items/{item_id}/vote", response_model=ItemVoteResponse)
async def vote_on_item(
    item_id: int,
    vote_request: ItemVoteRequest,
    db: Session = Depends(get_db)
):
    """
    Vote on a menu item (upvote or downvote).

    - **vote**: 'up' or 'down'

    Returns updated vote counts and rating percentage.
    """
    # Get the item
    item = db.query(Item).filter(Item.id == item_id).first()

    if not item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Item with id {item_id} not found"
        )

    # Update vote counts
    if vote_request.vote == "up":
        item.upvotes += 1
        item.total_votes += 1
    elif vote_request.vote == "down":
        item.total_votes += 1

    # Commit changes
    db.commit()
    db.refresh(item)

    # Calculate rating percentage
    rating_percentage = item.rating_percentage

    return ItemVoteResponse(
        item_id=item.id,
        upvotes=item.upvotes,
        total_votes=item.total_votes,
        rating_percentage=rating_percentage
    )
