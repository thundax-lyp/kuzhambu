from kuzhambu_workers.api.ai_usecase_routes import router


def test_usecase_route_descriptions_include_final_state_contract() -> None:
    documented = [
        route.description
        for route in router.routes
        if getattr(route, "path", "").startswith("/internal/ai/")
    ]

    assert documented
    assert all("failureStage" in description for description in documented)
    assert all("fallbackUsed" in description for description in documented)
    assert all("artifactReference" in description for description in documented)
