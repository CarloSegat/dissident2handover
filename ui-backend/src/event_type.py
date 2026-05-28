from pydantic import BaseModel, Field
from typing import Dict, Any

class Event(BaseModel):
    src_entity: str = Field(..., alias="SrcEntity")
    trg_entity: str = Field(..., alias="TrgEntity")
    timestamp: int = Field(..., alias="Timestamp")
    content: Dict[str, Any] = Field(..., alias="Content")
    group: str = Field(..., alias="Group")
    sub_group: str = Field(..., alias="SubGroup")

    class Config:
        allow_population_by_field_name = True
