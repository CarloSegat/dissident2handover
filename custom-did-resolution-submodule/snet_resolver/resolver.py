"""Snet/dissident2 DID resolver."""

import json
import logging
import os
import re
from typing import Pattern

import aiohttp
from aries_cloudagent.core.profile import Profile
from aries_cloudagent.resolver.base import (
    BaseDIDResolver,
    DIDNotFound,
    ResolverError,
    ResolverType,
)

LOGGER = logging.getLogger(__name__)


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
        if not custom_resolvement_cache_url:
            raise ResolverError("Environment variable CUSTOM_RESOLVEMENT_CACHE_URL not found or empty.")

        LOGGER.debug("resolving %s via %s", did, custom_resolvement_cache_url)
        payload = {"requestDid": did}  # controllers read eventData.get("requestDid")

        async with aiohttp.ClientSession() as session:
            async with session.post(custom_resolvement_cache_url, json=payload) as response:
                if response.status == 200:
                    res_txt = await response.text()
                    try:
                        return json.loads(res_txt)
                    except Exception as err:
                        LOGGER.warning("malformed resolver response for %s: %s", did, res_txt)
                        raise ResolverError("Response was incorrectly formatted") from err
                if response.status == 404:
                    raise DIDNotFound(f"No document found for {did}")
                raise ResolverError(
                    "Could not find doc for {}: {}".format(did, await response.text())
                )
