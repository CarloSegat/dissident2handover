"""ACA-Py Snet/dissident2 DID resolver plugin."""

import logging

from aries_cloudagent.config.injection_context import InjectionContext
from aries_cloudagent.resolver.did_resolver import DIDResolver
from aries_cloudagent.resolver.default.indy import IndyDIDResolver

from .resolver import SnetResolver

LOGGER = logging.getLogger(__name__)


async def setup(context: InjectionContext):
    """Setup the plugin."""
    registry = context.inject(DIDResolver)

    index_default_sov_resolver = None
    for index, resolver in enumerate(registry.resolvers):
        if type(resolver) == type(IndyDIDResolver()):
            index_default_sov_resolver = index

    if index_default_sov_resolver is not None:
        del registry.resolvers[index_default_sov_resolver]
    registry.register_resolver(SnetResolver())
    LOGGER.info("snet_resolver registered (replaced default IndyDIDResolver)")
