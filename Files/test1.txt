#include "Events.h"
#include "SFML/Graphics.hpp"

Events Event_Check(const sf::Event& event) {
	if (event.type == sf::Event::Closed) {
		return Events::Close;
	}
	else if (event.type == sf::Event::KeyPressed) {
		if (event.key.code == sf::Keyboard::Left) {
			return Events::Left;
		}
		if (event.key.code == sf::Keyboard::Right) {
			return Events::Right;
		}
		if (event.key.code == sf::Keyboard::Up) {
			return Events::Rotate;
		}
	}
}