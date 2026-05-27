"""Github Resolver."""

import json
import re
from typing import Pattern
import os
import requests

import aiohttp
from aries_cloudagent.core.profile import Profile
from aries_cloudagent.resolver.base import (
    BaseDIDResolver,
    DIDNotFound,
    ResolverError,
    ResolverType,
)
from pydid import DID


class SnetResolver(BaseDIDResolver):

    def __init__(self):
        super().__init__(ResolverType.NATIVE)
        self._supported_did_regex = re.compile("^did:sov:.*$")

    @property
    def supported_did_regex(self) -> Pattern:
        return self._supported_did_regex

    async def setup(self, context):
        pass
    async def _resolve(self, profile: Profile, did: str, service_accept=None) -> dict:

        custom_resolvement_cache_url = os.getenv('CUSTOM_RESOLVEMENT_CACHE_URL')
        print(f">>>>>>>>>>>>>> ACAPY_ENDPOINT env is {os.getenv('ACAPY_ENDPOINT')}")
        print(f">>>>> >>>>>> >>>>>>> >>>>>>>>> custom_resolvement_cache_url {custom_resolvement_cache_url}")
        print("okok")

        if custom_resolvement_cache_url:
            url_with_did = f"{custom_resolvement_cache_url}"
            payload = {"requestDid": did}  # controllers read eventData.get("requestDid")

            async with aiohttp.ClientSession() as session:
                async with session.post(url_with_did, json=payload) as response:
                    if response.status == 200:
                        try:
                            res_txt = await response.text()
                            print(f">>>>> >>>>>> >>>>>>> >>>>>>>>> res_txt res_txtres_txt res_txt res_txt {res_txt}")
                            return json.loads(res_txt)
                        except Exception as err:
                            print(f">>>>> >>>>>> >>>>>>> >>>>>>>>> Response was incorrectly formatted")
                            raise ResolverError(
                                "Response was incorrectly formatted"
                            ) from err
                    if response.status == 404:
                        print(f">>>>> >>>>>> >>>>>>> >>>>>>>>> 404 No document found for {did}")
                        raise DIDNotFound(f"No document found for {did}")
                    raise ResolverError(
                        "Could not find doc for {}: {}".format(did, await response.text())
                    )
        else:
            raise ResolverError("Environment variable CUSTOM_RESOLVEMENT_CACHE_URL not found or empty.")
