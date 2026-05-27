from pydantic import BaseModel, Json, Field
from datetime import datetime
from enum import Enum
from typing import Dict, Any

class Event(BaseModel):
    src_entity: str = Field(..., alias="SrcEntity")
    trg_entity: str = Field(..., alias="TrgEntity")
    # pair_id: str = Field(..., alias="PairID")
    timestamp: int = Field(..., alias="Timestamp")  
    content: Dict[str, Any] = Field(..., alias="Content")
    group: str = Field(..., alias="Group")
    sub_group: str = Field(..., alias="SubGroup")

    class Config:
        allow_population_by_field_name = True
