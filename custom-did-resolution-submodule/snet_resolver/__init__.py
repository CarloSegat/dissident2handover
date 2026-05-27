"""ACA-Py Resolver Github."""

from aries_cloudagent.config.injection_context import InjectionContext
from aries_cloudagent.resolver.did_resolver import DIDResolver

from .resolver import SnetResolver

from aries_cloudagent.resolver.default.indy import IndyDIDResolver


async def setup(context: InjectionContext):
    """Setup the plugin."""
    print("registering the snet reoslver custom >>>>>>>>>>>>>")
    registry = context.inject(DIDResolver)

    index_default_sov_resolver = None
    for index, resolver in enumerate(registry.resolvers):
        print(index, resolver)
        if type(resolver) == type(IndyDIDResolver()):
            print("got itititititiit ititit")
            index_default_sov_resolver = index

    if index_default_sov_resolver is not None:
        print("deregistering the default sov resolver")
        del registry.resolvers[index_default_sov_resolver]
    # assert isinstance(registry, DIDResolverRegistry)
    registry.register_resolver(SnetResolver())
